from pyparsing import *
from modules.stemming.porter2 import stem
import csv
import pickle
import os
from sets import Set
import pprint
pp = pprint.PrettyPrinter(indent=2)



os.chdir(os.path.dirname(__file__))

def Index(file_dataset = "papers.csv", file_dump = "output.p", id_col = 0, text_col = -1):

    try:
        arr = pickle.load(open(file_dump, "rb"))
        collection = arr[0]
        docs = arr[1]
        print "Retrieved index from file " + file_dump
    except (OSError, IOError) as e:
        collection = {}
        doc_nr = 0
        docs = {}

        with open(file_dataset, "r") as paperscsv:
            papersreader = csv.reader(paperscsv,delimiter=',',quotechar='"')
            for doc in papersreader:
                # skip the header
                if doc_nr > 0:
                    id = doc[id_col]
                    text = doc[text_col]
                    docs[id] = text
                    # extract tokens from the title and the text
                    # TODO: improve parser grammar to also include numbers
                    grammar = Word(alphas)
                    token_pos = 0
                    for token,start,end in grammar.scanString(text):
                        # token is still in array. get the first element to get the string, and then stem this.
                        token = stem(str(token[0]).lower())
                        token_pos += 1
                        doc_terms = []
                        if collection.has_key(token):
                            doc_dict = dict(collection[token])
                            if doc_dict.has_key(id):
                                doc_terms = doc_dict[id]
                            else:
                                collection[token][id] = doc_terms
                        else:
                            collection[token] = {id:doc_terms}
                            doc_terms.append(token_pos)
                doc_nr += 1

        pickle.dump([collection, docs], open(file_dump, "wb"))

        print "Retrieved index by building it from dataset: " + file_dataset

    return [collection,docs]

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
    docs = Index()[1]
    for item in tmp_result:
        if docs[item].count(search_string):
            result.add(item)
    return result

def GetNot(not_set):
    docs = Index()[1]
    all = Set(docs.keys())
    return all.difference(not_set)

if __name__ == "__main__":
    test = Index()

    print pp.pformat(test[0])
