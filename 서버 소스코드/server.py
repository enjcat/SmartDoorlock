import asyncio
import websockets
import time
import sqlite3
import socket

conn = sqlite3.connect('DoorLock.db', timeout=10)
cur = conn.cursor()

cur.execute('CREATE TABLE IF NOT EXISTS log( YEAR INTEGER, MONTH INTEGER, DAY INTEGER, TIME TEXT, ID TEXT, TIMESTRING INTEGER)')
cur.execute('CREATE TABLE IF NOT EXISTS user( UID text PRIMARY KEY, NAME text DEFAULT "name", ROLE text DEFAULT "role", PERM text DEFAULT "2")')
cur.execute('CREATE TABLE IF NOT EXISTS login( ID text PRIMARY KEY, PASSWORD text DEFAULT "0000", UID text, FOREIGN KEY (UID) REFERENCES user (UID))')


def make_timestring(year, month, day):
    return int(year + two_digit(month) + two_digit(day))


def two_digit(text):
    if len(text) < 2:
        return '0' + text
    else:
        return text


def get_ipaddress():
    #s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    #s.connect(("gmail.com",80))
    #r = s.getsockname()[0]
    #s.close()
    return "192.168.1.180"


async def Account_Lookup(websocket):
    global cur, conn
    count, message = 0, ''
    while count < 4:
        message += await websocket.recv()
        count += 1
    print("입력받은 UID값 : ", message)
    cur.execute("SELECT UID FROM user WHERE UID=(?)",(message, ))
    exist = cur.fetchone()
    print("DB 탐색중...")
    if exist is not None:
        await websocket.send('1')
        print("회원입니다 문을 엽니다.")
        # 로그파일 저장
        now = time.localtime(time.time())  # 현재시각을 가져온다.
        cur.execute('INSERT INTO log VALUES(?, ?, ?, ?, ?, ?)', (now.tm_year, now.tm_mon, now.tm_mday,
                                                                 str(now.tm_hour)+':'+str(now.tm_min)+':'+str(now.tm_sec), message, make_timestring(str(now.tm_year), str(now.tm_mon), str(now.tm_mday))))
        conn.commit()
    else:
        await websocket.send('0')
        print("비회원입니다 문을 열지 않습니다.")
    print("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ")


async def Account_Register(websocket):
    global cur, conn
    count, message = 0, ''
    while count < 4:
        message += await websocket.recv()
        count += 1
    print("입력받은 UID값 : ", message)
    cur.execute("SELECT UID FROM user WHERE UID=(?)",(message, ))
    exist = cur.fetchone()
    print("DB 탐색중...")
    if exist is not None:
        await websocket.send('3')
        print("이미 회원입니다.")
    else:
        await websocket.send('2')
        print("회원이 아닙니다. ID를 등록합니다.")
        cur.execute('INSERT INTO user(UID) VALUES(?)', (message, ))
        conn.commit()
    print("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ")


def Webcam_streaming(websocket):
    pass


def Webcam_Capturing(websocket):
    pass


async def LogLookUp(websocket):
    global cur, conn
    count, start, end = 0, '', ''
    while count < 2:
        if count == 0:
            start = await websocket.recv()
        if count == 1:
            end = await websocket.recv()
        count += 1
    print(start, " ~ ", end, "까지의 로그조회.")
    start = start.split('-')
    end = end.split('-')
    cur.execute("SELECT * FROM log l, user u WHERE (l.TIMESTRING BETWEEN ? AND ?) AND l.ID == u.UID",
                (make_timestring(start[0], start[1], start[2]), make_timestring(end[0], end[1], end[2])))
    for i in cur:
        print(i)
        print(str(i[0])+'년 '+str(i[1])+'월 '+str(i[2])+'일 '+i[3]+' - '+i[8] + '\n     uid :' + i[4])
        await websocket.send(str(i[0])+'년 '+str(i[1])+'월 '+str(i[2])+'일 '+i[3]+' - '+i[8] + '\n     uid :' + i[4])
    await websocket.send('End')
    print("로그를 보냈습니다.")
    print("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ")


def photoLookUp(websocket):
    pass


def UserLookUp(websocket):
    pass


async def Login(websocket):
    global cur, conn
    ID = await websocket.recv()
    Pw = await websocket.recv()
    print("입력받은 ID값 : ", ID)
    cur.execute("SELECT * FROM login, user WHERE ID=(?) AND login.UID = user.UID",(ID, ))
    exist = cur.fetchone()
    print("DB 탐색중...")
    if exist is not None:
        print("ID 값 일치. ")
        if exist[1] == Pw:
            await websocket.send(exist[2] + ' ' + exist[6] + ' ' + exist[4] + " " + exist[5])
            print("PassWord 값 일치. ", exist[0], "의 로그인을 허락합니다.")
        else:
            await websocket.send('false')
            print("PassWord 값 불일치. ", exist[0], "의 로그인을 불허합니다.")
    else:
        await websocket.send('false')
        print("ID 값 불일치. 로그인을 불허합니다.")
    print("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ")


async def RegID(websocket):
    # uuid, 이름, 역할 ,퍼미션, 아이디, 비밀번호를 user와 login 테이블에 업데이트한다
    global cur, conn
    uid = await websocket.recv()
    name = await websocket.recv()
    role = await websocket.recv()
    perm = await websocket.recv()
    id = await websocket.recv()
    passwd = await websocket.recv()
    print('입력받은 uid값 : ', uid)
    print('입력받은 ID값 : ', id)
    cur.execute("UPDATE user SET name = (?), role = (?), perm = (?) WHERE UID = (?)", (name, role, perm, uid))
    cur.execute('INSERT INTO login VALUES(?, ?, ?) ON CONFLICT(ID) DO UPDATE SET PASSWORD = (?)', (id, passwd, uid, passwd))
    conn.commit()
    await websocket.send("true")
    print("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ")


async def IdenPerm(websocket):
    global cur, conn
    # 아이디를 가지고있는 로그인 테이블이랑 퍼미션이 있는 유저 테이블이랑 조인하여 퍼미션 확인
    id = await websocket.recv()
    print('입력받은 아이디 : ', id)
    cur.execute('select perm from user, login where id = (?) and login.uid = user.uid', (id))
    perm = []
    for i in cur:
        perm.append(i[0])

    # perm을 클라이언트로 전송
    await websocket.send(perm[0])
    print("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ")

async def SearchUser(websocket):
    global cur, conn
    uid = await websocket.recv()
    cur.execute('select * from user, login where login.uid = (?) and login.uid = user.uid', (uid, ))
    exist = cur.fetchone()
    if exist is not None:  # 회원정보와 로그인정보가 모두있는경우
        print(exist[1] + " " + exist[2] + " " + exist[3] + " " + exist[4] + " " + exist[5])
        await websocket.send(exist[1] + " " + exist[2] + " " + exist[3] + " " + exist[4] + " " + exist[5])
    else:
        cur.execute('select * from user where user.uid = (?)', (uid, ))
        exist = cur.fetchone()
        if exist is not None:  # 회원정보만 있는경우 아이디와 비밀번호는 None으로 보내자
            print(exist[1] + " " + exist[2] + " " + exist[3] + " " + '생성필요' + " " + '생성필요')
            await websocket.send(exist[1] + " " + exist[2] + " " + exist[3] + " " + '생성필요' + " " + '생성필요')
        else:
            # 회원정보가 없는경우
            await websocket.send('false')
    print("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ")

async def echo(websocket, path):
    message = await websocket.recv()
    print("클라이언트가 연결되었습니다.")

    # try:
    if message == '1':
        print('문열기 기능을 수행합니다.')
        await Account_Lookup(websocket)
    elif message == '2':
        print('사용자등록 기능을 수행합니다.')
        await Account_Register(websocket)
    elif message == '3':
        print('Webcam_streaming')
    elif message == '4':
        print('Webcam_Capturing')
    elif message == '5':
        print('로그조회 기능을 수행합니다.')
        await LogLookUp(websocket)
    elif message == '6':
        print('photoLookUp')
    elif message == '7':
        print('유저 정보 조회')
        await SearchUser(websocket)
    elif message == '8':
        print('로그인 기능을 수행합니다.')
        await Login(websocket)
    elif message == '9':
        print('아이디와 비밀번호를 설정합니다.')
        # uuid, 이름, 역할 ,퍼미션, 아이디, 비밀번호를 받아 user와 login 테이블에 업데이트한다
        await RegID(websocket)
    elif message == '10':
        print('권한을 확인합니다.')
        # 받은 유저의 권한을 확인 후 전송
        await IdenPerm(websocket)
    # except:
    #   print("Unexpected error:", sys.exc_info()[0])


print("Server start: "+get_ipaddress())

port = 180
asyncio.get_event_loop().run_until_complete(websockets.serve(echo, get_ipaddress(), port, close_timeout=100))
asyncio.get_event_loop().run_forever()
