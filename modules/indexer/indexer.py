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
import pprint

from collections import OrderedDict

pp = pprint.PrettyPrinter(indent=4)

os.chdir(os.path.dirname(__file__))

file_dump = "../../data/derived/"

def Index(file_dataset = "../../data/papers.csv", file_dump = "../../data/derived/", id_col = 0, text_col = -1, title_col = 2):

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
                title = doc[title_col]
                text = doc[text_col]
                # extract tokens from the title and the text
                token_pos = 0
                print "Examining doc: " + str(id)
                tokenized_text = Word(alphas).searchString(title)
                docs[id] = len(tokenized_text)
                for token in tokenized_text:
                    token = stem(str(token[0]).lower())
                    token_pos += 1
                    collection[token][id].append(token_pos)
                tokenized_text = Word(alphas).searchString(text)
                docs[id] += len(tokenized_text)
                for token in tokenized_text:
                    token = stem(str(token[0]).lower())
                    token_pos += 1
                    collection[token][id].append(token_pos)
                doc_nr += 1
                # if doc_nr > 10:
                #     break

        print "Calculating idf..."

        for term in collection.keys():
            idf[term] = math.log10(doc_nr/float(len(collection[term])))

        print "Dumping index..."

        pickle.dump(collection,open(file_dump + "index.lol", "wb"))

        print "Dumping idf..."

        pickle.dump(idf, open(file_dump + "idf.lol", "wb"))

        print "Dumping doc_length..."

        pickle.dump(docs, open(file_dump + "doc_length.lol", "wb"), pickle.HIGHEST_PROTOCOL)

    return [collection, idf, docs]

def CreateDbIndex():
    arr = Index()
    collection = arr[0]
    idf = arr[1]

    print "Dumping index to database..."

    with sqlite3.connect(file_dump + "index.sqlite") as dump_db:
        cursor = dump_db.cursor()
        cursor.execute("drop table if exists vocabulary")
        cursor.execute("CREATE TABLE IF NOT EXISTS vocabulary (term text PRIMARY KEY, posting blob, idf real)")
        for term in collection.keys():
            posting_data = pickle.dumps(collection[term], pickle.HIGHEST_PROTOCOL)
            cursor.execute("insert into vocabulary values (?,?,?)", (term, sqlite3.Binary(posting_data), idf[term]))

def GetDbPostings(terms):
    sql = ""
    for t in terms:
        if len(sql) == 0:
            sql = "select * from vocabulary where term like '" + t + "%'"
        else:
            sql += " or term like '" + t + "%'"

    result = {}

    with sqlite3.connect(file_dump + "index.sqlite") as index_db:
        cursor = index_db.cursor()
        cursor.execute(sql)
        for row in cursor.fetchall():
            posting = pickle.loads(str(row[1]))
            term = str(row[0])
            result[term] = posting

    return result

def GetDbIdf(terms):
    sql = ""
    for t in terms:
        if len(sql) == 0:
            sql = "select * from vocabulary where term='" + t + "'"
        else:
            sql += " or term='" + t + "'"

    result = {}

    with sqlite3.connect(file_dump + "index.sqlite") as index_db:
        cursor = index_db.cursor()
        cursor.execute(sql)
        for row in cursor.fetchall():
            term = str(row[0])
            result[term] = row[2]

    return result

def GetDbDocs(terms):
    docs = pickle.load(open(file_dump + "doc_length.lol", "rb"))
    return docs

def GetPapersBy(authorid):
    result = []
    with sqlite3.connect(os.getcwd() + '/../../data/database.sqlite') as database:
        cursor = database.cursor()
        cursor.execute("select paper_id from paper_authors where author_id='" + authorid + "'")
        for row in cursor.fetchall():
            result.append(row[0])
    return result

def GetPapersIn(year):
    result = []
    with sqlite3.connect(os.getcwd() + '/../../data/database.sqlite') as database:
        cursor = database.cursor()
        cursor.execute("select id from papers where year=" + year)
        for row in cursor.fetchall():
            result.append(row[0])
    return result

def GetWord(word):
    #index = Index()[0]
    index = GetDbPostings([word])
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

"""
*Assuming query is already stemmed*
Usage:
    GetQuotesExact("a big cake")
Output:
    List of documents, containing the exact phrase, ranked by the number of phrases contained by the
"""

def GetQuotesExact(search_string):
    index = Index()[0]
    search_words = search_string.split(" ")
    for sw in search_words:
        # If one/more words not in index, return empty result set
        # print "Index of {} is {}".format(sw, dict(index[sw]))
        if sw not in index:
            return []
    posting_list = index[search_words[0]]
    for sw in search_words[1:]:
        posting_list = merge(posting_list, index[sw], 1)
    # Sort docs based on number of matches
    ranked_docs = OrderedDict(sorted(posting_list.viewitems(), key=lambda x: len(x[1])))
    print "Merged indices: ", pp.pformat(posting_list)
    result = list(reversed(ranked_docs))
    return result

def merge(posting_list1, posting_list2, k=1):
    merged_list = {}
    intersected_docIDs = sorted(set(posting_list1.keys()) & set(posting_list2.keys()))
    print "Potential document IDs: ", intersected_docIDs
    for docID in intersected_docIDs:
        positions1 = posting_list1[docID]
        positions2 = posting_list2[docID]
        i = j = 0
        res = []
        # print "docID = {}, positions1 = {}, positions2 = {}".format(docID, positions1, positions2)
        while i < len(positions1) and j < len(positions2):
            # print "comparing {}, and {} from document {}".format(positions1[i], positions2[j], docID)
            if (positions1[i] + k == positions2[j]):
                res.append(positions2[j])
                i = i + 1
                j = j + 1
            elif positions1[i] < positions2[j]:
                i = i + 1
            else:
                j = j + 1
        if(res != []):
            merged_list[docID] = res
    return merged_list

def GetQuotes(search_string, tmp_result):
    result = Set()
    index = Index()[0]
    for item in tmp_result:
        if docs[item].count(search_string):
            result.add(item)
    return result

def Search(query, type = "BM25"):

    """
    This method searches in the corpus using VSM or BM25
    :param query: query to search in the corpus
    :param type: VSM or BM25
    :return: list of doc ids ranked by their similarity to query
    """

    # get tokens from query
    q_grammar = Word(alphas)
    q_tf = {}
    q_length = 0
    for token, start, end in q_grammar.scanString(query):
        token = stem(str(token[0]).lower())
        if q_tf.has_key(token):
            q_tf[token] += 1
        else:
            q_tf[token] = 1
        q_length += 1

    # index = Index()
    # vocabulary = index[0]
    # d_idf = index[1]
    # d_length = index[2]
    q_terms = q_tf.keys()
    vocabulary = GetDbPostings(q_terms)
    d_idf = GetDbIdf(q_terms)
    d_length = GetDbDocs(q_terms)

    q_score = 0
    d_score_list = {}
    first_pos = {}

    d_avg = 0
    for doc in d_length:
        d_avg += d_length[doc]

    d_count = len(d_length)
    d_avg = d_avg / d_count

    # get the collection's tf idf with the query's tf idf
    for term in q_terms:
        if d_idf.has_key(term):
            q_score = q_tf[term] * d_idf[term]
            for doc in vocabulary[term]:
                posting = vocabulary[term][doc]
                d_tf = len(posting)
                if type == "BM25":
                    k = 2.0
                    b = 0.75
                    d_score = (((k + 1) * d_tf)/((k * (1-b+b*(d_count/d_avg))) + d_tf)) * d_idf[term] * q_score
                if type == "VSM":
                    d_score = d_tf * d_idf[term] * q_score
                if d_score_list.has_key(doc):
                    d_score_list[doc] += d_score
                else:
                    d_score_list[doc] = d_score

    #print q_score
    #print sorted(d_score_list.items(), key=operator.itemgetter(0))

    # normalize the weights of the documents
    if type == "VSM":
        for d in d_score_list:
            d_score_list[d] = d_score_list[d] / d_length[d]

    # sort the results descendingly
    sorted_scores = sorted(d_score_list.items(), key=operator.itemgetter(1), reverse=True)

    #print sorted(d_score_list.items(), key=operator.itemgetter(0))
    #print sorted_scores

    result = []
    for doc, weight in sorted_scores:
        result.append(str(doc))

    #print result

    return result

def GetNot(not_set):
    docs = Index()[1]
    all = Set(docs.keys())
    return all.difference(not_set)

if __name__ == "__main__":
    # GetDbPostings(["latent", "dirichlet"])
    # GetDbIdf(["latent", "dirichlet"])

    #print pp.pformat(test[0])
    #print pp.pformat(test[1])
    #index = Index()[0]

    database = sqlite3.connect(os.getcwd() + '/../../data/database.sqlite')
    cursor = database.cursor()

    cursor.execute("SELECT * from papers where id = 2")
    #
    # for row in cursor.fetchall():
    #     print row
    #
    # result = Search("data mining", "VSM")
    #
    # print len(result)
    #
    # with open("test.txt", "a") as resultfile:
    #     # resultfile.writelines("type: Positional Boolean, query: Latent Dirichlet\n")
    #     # result = GetQuotesExact("latent dirichlet")
    #     # counter = 1
    #     # for doc in result:
    #     #     cursor.execute("select title from papers where id=" + str(doc))
    #     #     for row in cursor.fetchall():
    #     #         if counter <= 21:
    #     #             print str(counter) + ". " + row[0]
    #     #             resultfile.writelines(str(counter) + ". " + row[0])
    #     #         else:
    #     #             break
    #     #     counter += 1
    #     resultfile.writelines("type: VSM, query: Latent Dirichlet\n")
    #     result = Search("Latent Dirichlet", "VSM")
    #     counter = 1
    #     for doc in result:
    #         cursor.execute("select title from papers where id=" + str(doc))
    #         for row in cursor.fetchall():
    #             if counter <= 21:
    #                 print str(counter) + ". " + row[0]
    #                 resultfile.writelines(str(counter) + ". " + row[0] + "\n")
    #             else:
    #                 break
    #         counter += 1
    #     resultfile.writelines("type: BM25, query: Latent Dirichlet\n")
    #     result = Search("Latent Dirichlet", "BM25")
    #     counter = 1
    #     for doc in result:
    #         cursor.execute("select title from papers where id=" + str(doc))
    #         for row in cursor.fetchall():
    #             if counter <= 21:
    #                 print str(counter) + ". " + row[0]
    #                 resultfile.writelines(str(counter) + ". " + row[0] + "\n")
    #             else:
    #                 break
    #         counter += 1
    #     resultfile.writelines("type: VSM, query: Relevance Feedback\n")
    #     result = Search("Relevance Feedback", "VSM")
    #     counter = 1
    #     for doc in result:
    #         cursor.execute("select title from papers where id=" + str(doc))
    #         for row in cursor.fetchall():
    #             if counter <= 21:
    #                 print str(counter) + ". " + row[0]
    #                 resultfile.writelines(str(counter) + ". " + row[0] + "\n")
    #             else:
    #                 break
    #         counter += 1
    #     resultfile.writelines("type: BM25, query: Relevance Feedback\n")
    #     result = Search("Relevance Feedback", "BM25")
    #     counter = 1
    #     for doc in result:
    #         cursor.execute("select title from papers where id=" + str(doc))
    #         for row in cursor.fetchall():
    #             if counter <= 21:
    #                 print str(counter) + ". " + row[0]
    #                 resultfile.writelines(str(counter) + ". " + row[0] + "\n")
    #             else:
    #                 break
    #         counter += 1
    #
    #     #Search("Deep Learning", "VSM")
    #     #Search("Deep Learning", "BM25")
    #     #print("----------")
    #     #a = {1: [1, 10, 20, 30], 2:[1, 15, 16, 17], 3:[4, 17, 19, 80]}
    #     #b = {1: [1, 11, 20, 31], 2:[1, 15, 16, 17], 3:[4, 17, 19, 80]}
    #     #print("A:")
    #     #pp.pprint(a)
    #     #print("B:")
    #     #pp.pprint(b)
    #     #r = merge(a, b)
    #     #print("result:", r)
    #     #print("-------------")
    #
    # database.close()
