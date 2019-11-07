import sqlite3
import time
# print(sqlite3.version)
# print(sqlite3.sqlite_version)
# now = time.localtime(time.time())
conn = sqlite3.connect('DoorLock.db')
cur = conn.cursor()

# cur.execute('drop table user')
# cur.execute('drop table log')
# cur.execute('drop table login')

cur.execute('CREATE TABLE IF NOT EXISTS log( YEAR INTEGER, MONTH INTEGER, DAY INTEGER, TIME TEXT, ID TEXT, TIMESTRING INTEGER)')
cur.execute('CREATE TABLE IF NOT EXISTS user( UID text PRIMARY KEY, NAME text DEFAULT "name", ROLE text DEFAULT "role", PERM text DEFAULT "2")')
cur.execute('CREATE TABLE IF NOT EXISTS login( ID text PRIMARY KEY, PASSWORD text DEFAULT "0000", UID text, FOREIGN KEY (UID) REFERENCES user (UID))')
'''
cur.execute("SELECT name FROM sqlite_master WHERE type='table';")
for row in cur:
    print(row[0])
'''

# cur.execute('DELETE FROM user WHERE ID = "2016154045"')

# cur.execute('INSERT INTO user VALUES("0000", "root", "관리자", "0")')
# cur.execute('INSERT INTO login VALUES("root", "0000", "0000")')
# cur.execute('INSERT INTO user VALUES("0001", "신형만", "아빠", "1")')
# cur.execute('INSERT INTO login VALUES("DAD", "1111", "0001")')
# cur.execute('INSERT INTO user VALUES("0002", "봉미선", "엄마", "1")')
# cur.execute('INSERT INTO login VALUES("MOM", "2222", "0002")')
# cur.execute('INSERT INTO user VALUES("0003", "신짱구", "아들", "2")')
# cur.execute('INSERT INTO login VALUES("BOY", "3333", "0003")')
# cur.execute('INSERT INTO user VALUES("0004", "신짱아", "딸", "2")')
# cur.execute('INSERT INTO login VALUES("GIRL", "4444", "0004")')


#cur.execute('INSERT INTO login VALUES("kwan", "0000", "24049152163")')
# cur.execute("IF EXISTS (SELECT ID FROM login WHERE ID = (?)) UPDATE login SET PASSWORD = (?) WHERE ID = (?) ELSE INSERT INTO login VALUES(?, ?, ?)", (id, passwd, id, id, passwd, uid))

# id = 'kwan'
# name = 'kwaneung'
# role = 'first'
# perm = '1'
# passwd = '1234'
# uid = '24049152163'
#
# cur.execute("UPDATE user SET name = (?), role = (?), perm = (?) WHERE UID = (?)", (name, role, perm, uid))
# cur.execute('INSERT INTO login VALUES(?, ?, ?) ON CONFLICT(id) DO UPDATE SET PASSWORD = (?)', (id, passwd, uid, passwd))
conn.commit()

print('----user-----------------------')
cur.execute('select * from user')
for i in cur:
    print(i)


print('----log-----------------------')
cur.execute('select * from log')
for i in cur:
    print(i)

print('----login-----------------------')
cur.execute('select * from login')
for i in cur:
    print(i)

# cur.execute('INSERT INTO user VALUES("24049152163", "홍길동", "아빠", "0")')
# conn.commit()
