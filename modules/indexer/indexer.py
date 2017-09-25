from pyparsing import *
from modules.stemming.porter2 import stem
import csv
import sys
import pickle

file_input = sys.argv[1]
file_output = sys.argv[2]

id_col = 0
text_col = -1

doc_nr = 0

collection = {}

with open(file_input, "r") as paperscsv:
    papersreader = csv.reader(paperscsv,delimiter=',',quotechar='"')
    for doc in papersreader:
        # skip the header
        if doc_nr > 0:
            id = doc[id_col]
            text = doc[text_col]
            # extract tokens from the title and the text
            # TODO: improve parser grammar to also include numbers
            grammar = Word(alphas)
            token_pos = 0
            for token,start,end in grammar.scanString(text):
                # token is still in array. get the first element to get the string, and then stem this.
                # TODO: improve the stemming to also normalize plural words
                token = stem(str(token[0])).lower()
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

print(collection)

pickle.dump(collection, open(file_output, "wb"))
