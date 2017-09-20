import os
import sqlite3

database = sqlite3.connect(os.getcwd() + '/../../data/database.sqlite')
cursor   = database.cursor()

cursor.execute('SELECT * FROM authors')

for row in cursor.fetchall():
    print row

database.close()
