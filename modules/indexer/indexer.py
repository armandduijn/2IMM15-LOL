from pyparsing import *
from modules.stemming.porter2 import stem
import csv
import cPickle as pickle
import os
from sets import Set
import pprint
import math
import operator
import sqlite3
from collections import defaultdict
import functools

pp = pprint.PrettyPrinter(indent=2)



os.chdir(os.path.dirname(__file__))


def Index(file_dataset = "../../data/papers.csv", file_dump = "../../data/derived/", id_col = 0, text_col = -1):

    try:
        collection = pickle.load(open(file_dump + "index.lol", "rb"))
        idf = pickle.load(open(file_dump + "idf.lol", "rb"))
        docs = pickle.load(open(file_dump + "doc_length.lol", "rb"))
        #print "Retrieved index from file " + file_dump
    except:
        collection = defaultdict(functools.partial(defaultdict, list))
        doc_nr = 0
        idf = {}
        docs  = {}

        print "Building indexer..."

        #with open(file_dataset, "r") as paperscsv:
            #papersreader = csv.reader(paperscsv,delimiter=',')
        with sqlite3.connect(os.getcwd() + '/../../data/database.sqlite') as database:
            cursor = database.cursor()
            cursor.execute('SELECT * FROM papers')
            for doc in cursor.fetchall():
            #for doc in papersreader:
                # skip the header
                #if doc_nr > 0:
                id = doc[id_col]
                text = doc[text_col]
                # extract tokens from the title and the text
                token_pos = 0
                print "Examining doc: " + str(id)
                tokenized_text = Word(alphas).searchString(text)
                docs[id] = len(tokenized_text)
                for token in tokenized_text:
                    token = stem(str(token[0]).lower())
                    token_pos += 1
                    collection[token][id].append(token_pos)
                doc_nr += 1

        print "Calculating idf..."

        for term in collection.keys():
            idf[term] = math.log10(doc_nr/float(len(collection[term])))

        print "Dumping index..."

        pickle.dump(collection,open(file_dump + "index.lol", "wb"))

        print "Dumping idf..."

        pickle.dump(idf, open(file_dump + "idf.lol", "wb"))

        print "Dumping doc_length..."

        pickle.dump(docs, open(file_dump + "doc_length.lol", "wb"))

    return [collection, idf, docs]

def GetWord(word):
    index = Index()[0]
    if (index.has_key(word)):
        return Set(index[word].keys())
    else:
        return Set()

def GetWordWildcard(word):
    result = Set()
    index = Index()[0]
    for item in index.keys():
        if word == item[0:len(word)]:
            result = result.union(Set(index[item].keys()))
    return result

def GetQuotes(search_string, tmp_result):
    result = Set()
    index = Index()[0]
    for item in tmp_result:
        if docs[item].count(search_string):
            result.add(item)
    return result

def VSMSearch(query):
    # this function returns the result based on vector space model
    # the result contains of doc ids sorted by similarity to query, and the first position of relevant token in doc

    print "query: " + query

    # get tokens from query
    q_grammar = Word(alphas)
    q_tf = {}
    q_length = 0
    for token, start, end in q_grammar.scanString(query):
        token = stem(str(token[0]).lower())
        token = unicode(token, "utf-8")
        print "stemmed token: " + token
        if q_tf.has_key(token):
            q_tf[token] += 1
        else:
            q_tf[token] = 1
        q_length += 1

    index = Index()
    vocabulary = index[0]
    d_idf = index[1]
    d_length = index[2]
    q_score = 0
    d_score_list = {}
    first_pos = {}

    # get the collection's tf idf with the query's tf idf
    k = 0
    for term in q_tf.keys():
        if d_idf.has_key(term):
            q_score = q_tf[term] * d_idf[term]
            for doc in vocabulary[term]:
                posting = vocabulary[term][doc]
                d_score = len(posting) * d_idf[term] * q_score
                if d_score_list.has_key(doc):
                    d_score_list[doc] += d_score
                else:
                    d_score_list[doc] = d_score
                    first_pos[doc] = posting[0]

    #print q_score
    #print sorted(d_score_list.items(), key=operator.itemgetter(0))

    # normalize the weights of the documents
    for d in d_score_list:
        d_score_list[d] = d_score_list[d] / d_length[d]

    # sort the results descendingly
    sorted_scores = sorted(d_score_list.items(), key=operator.itemgetter(1), reverse=True)

    #print sorted(d_score_list.items(), key=operator.itemgetter(0))
    #print sorted_scores
    print "length of result: " + str(len(sorted_scores))

    # include the first position of query term as found in the posting for each doc

    database = sqlite3.connect(os.getcwd() + '/../../data/database.sqlite')
    cursor = database.cursor()

    result = []
    counter = 1
    for doc, weight in sorted_scores:
        result.append([doc, first_pos[doc]])
        cursor.execute("select title from papers where id=" + str(doc))
        for row in cursor.fetchall():
            print str(counter) + ". " + row[0] + ", score: " + str(weight)
        counter += 1

    database.close()

    #print result

    return result

def GetNot(not_set):
    docs = Index()[1]
    all = Set(docs.keys())
    return all.difference(not_set)

if __name__ == "__main__":
    #test = Index()

    #print pp.pformat(test[0])
    #print pp.pformat(test[1])

    VSMSearch("Latent Dirichlet")
