##### this is for parse the abstract part from the database and then build the dictionary and the corpus on it, to use LDA model##
###input file: the whole databasse
### output file:corpus.mm',,,,alltoken.dict,,,,docidtokens.txt
from pyparsing import *
from modules.stemming.porter2 import stem
from multiprocessing import Process, freeze_support
import warnings
warnings.filterwarnings("ignore")
import csv
import sys
import json
import sqlite3
import os
import nltk
nltk.download('wordnet')
import pickle
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem.wordnet import WordNetLemmatizer
from gensim import corpora, models, similarities, utils
from nltk.stem.lancaster import LancasterStemmer
st = LancasterStemmer()
lm = WordNetLemmatizer()

    # here I am gonna build a stopwords list from the english stopwords (and maybe the nips paper words)
stops = set(stopwords.words("english"))

## this stop words are not supposed to be removed, but why?
## Because we have already used TFIDF model to judge the word,
## so the importance of each word will be give by LDA model automatically
# nips_stopwords = [u"ii", u"iii", u"et", u"al", u"data", u"sample", u"setting", u"paper", u"graph", u"model", u"network", u"use", u"learning", u"algorithm", u"method", u"problem", u"learn", u"input", u"functions", u"function", u"theorem", u"lemma"]
# for nips_stop in nips_stopwords:
#     stops.add(nips_stop)

#print stops
    #for paper_stop in nips_stopwords:
    #   stops.add(nips_stop)
    # """"
    # test the results from different tools and compare it to the english stopwords,
    # and I decide to use lemmatizer from nltk library
    # print stops
    # strrr = "themselves"
    # print "stem  " + stem(strrr)
    # print "st.stem  " + st.stem(strrr)
    # print "lm.lemmatize  " + lm.lemmatize(strrr)
    # """

 # creat a list to save the tokens from all abstracts
all_token_1 = []
id_token = {}
c = 0
list_id = []
recogonized_id = []

    ######here I test the code with a toy subset###########
# with open("toy.csv", "r") as paperscsv:
#     papersreader = csv.reader(paperscsv,delimiter=',',quotechar='"')
#     for doc in papersreader:
#         # skip the header
#         next(papersreader, None)

   ##### here I test the whole database#######
with sqlite3.connect(os.getcwd() + '/../../data/database.sqlite') as database:
    cursor = database.cursor()
    cursor.execute('SELECT * FROM papers')
    for doc in cursor.fetchall():

        # get document id
        id = doc[0]
        # get the all paper text
        list_id.append(id)
        title = doc[2].lower()
        text0 = doc[-1]
        text0 = text0.lower()
        text = title
        text = str(text)

    # # test the quality of the corpus
    #     if len(text) > 1:
    #         recogonized_id.append(id)
    #         c += 1
    # print c
    # print set(recogonized_id).symmetric_difference(list_id)
    # it turns out this way will miss about 20 papers from 6600 papers(which actually don't have an abstract), I think this is acceptable.

        # first, get the tokenized text
        tokenized_text = text.split(' ')
        # create a list to save all the nett tokens from one document
        nett_token = []
        for token in tokenized_text:
            # here token in tokenized_text is actually <class 'pyparsing.ParseResults'>,
            # want the first/only item in it which is  a string, then I can lemmatize it

            token = lm.lemmatize(token)
            # print token ###capable
            # print len(token) ###7
            if (token not in stops) and (len(token) > 1 ):
                nett_token.append(token)
        # after having the list of tokens,
        # build a list with out the document id
        all_token_1.append(nett_token)
        # build a dictionay with the  document id
        id_token[id] = nett_token
    print all_token_1
    # now I have pure words from all documents(abstract) : ) save them save them save them!
    # with open("alltokens.txt", 'w') as f:
    #     json.dump(all_token_1, f)
    with open('./topics/docidtokens.txt', 'w') as f:  # {"10":["mean","ddd"]....}
        json.dump(id_token, f)
        # print id_token #{1:['self','organization']}
    dictionary = corpora.Dictionary(all_token_1)
    dictionary.save('./topics/alltoken.dict')
    corpus = [dictionary.doc2bow(docs) for docs in all_token_1]
    corpora.MmCorpus.serialize('./topics/corpus.mm', corpus)      #rint corpus #[[(0,1),(1,1)]]


