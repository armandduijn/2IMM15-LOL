import json
import sqlite3
import os



with sqlite3.connect(os.getcwd() + 'database.sqlite') as database:#/../../data/ same reason, new database, can't be uploaded
    cursor = database.cursor()
    cursor.execute('SELECT * FROM papers')

####get the year_doc dictionary#####
doc_col = 0
year_col = 1
year_doc = {}
for doc in cursor.fetchall():
    doc_id = doc[0]
    year_id = doc[1]
    if year_id not in year_doc.keys():
        year_doc[year_id] = []
    year_doc[year_id].append(doc_id)
with open('./topics/year_doc.txt','w') as f:
    json.dump(year_doc,f)
#print year_doc
#
# ###get the mapping between year and topic####
with open('./topics/docid_topicid.txt', 'r') as f:
    doc_topic = json.load(f)
#print doc_topic["1"]

###
year_topic = {}
for year in year_doc.keys():
    topic_list = []
    for docs in year_doc[year]:
        docs = str(docs)
        topic_id = doc_topic[docs][0]
        topic_list.append(topic_id)
    year_topic[year] = topic_list
with open('./topics/year_topicid.txt','w') as f:
    json.dump(year_topic,f)












