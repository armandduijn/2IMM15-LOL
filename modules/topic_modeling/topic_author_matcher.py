import gensim
import json
import csv
import os
### input file: topicid_docid.txt',docid_topicid.txt, database(doc_author.csv)
### output file: authorid_topics.txt, topicid_authors.txt, authorid_docs.txt,  docid_authors.txt
with open("./topics/docid_topicid.txt",'r') as f:
    docid_topics = json.load(f)
    #print type(docid_topics)
with open("./topics/topicid_docid.txt", 'r') as f:
    topicid_docs = json.load(f)

with open("doc_author.csv",'r') as f:
    f = csv.reader(f,delimiter=',')
    next(f,None)   #skip the header
    docid_authors = {}
    authorid_docs = {}
    for doc in f:  #for each line
        paper_id = doc[1]
        author_id = doc[2]
        if paper_id not in docid_authors.keys():
            docid_authors[paper_id] = []
        docid_authors[paper_id].append(author_id)
        if author_id not in authorid_docs.keys():
            authorid_docs[author_id] = []
        authorid_docs[author_id].append(paper_id)
    # print docid_authors
    # print authorid_docs
    with open('./topics/docid_authors.txt','w') as b: # {'5988': ['7424', '3963'], ...
        json.dump(docid_authors,b)
    with open('./topics/authorid_docs.txt','w') as b: # {'5988': ['7424', '3963'], ...
        json.dump(authorid_docs,b)

#####map topic to author####
topicid_authors = {}
for topic in topicid_docs.keys():
    authors = []
    for doc in topicid_docs[topic]:
        doc = str(doc)
        if doc in docid_authors.keys():
            authors.extend(docid_authors[doc])
    topicid_authors[topic] = authors
with open('./topics/topicid_authors.txt','w') as b:
    json.dump(topicid_authors,b)

#####map author to topics#####
authorid_topics = {}
for author in authorid_docs.keys():
    topics = []
    for doc in authorid_docs[author]:
        if doc in docid_topics.keys():
            topics.extend(docid_topics[doc])
    topicid_authors[author] = topics
with open('./topics/authorid_topics.txt','w') as b:
    json.dump(authorid_topics,b)




