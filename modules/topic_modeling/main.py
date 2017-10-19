from pyparsing import *
from modules.stemming.porter2 import stem
from multiprocessing import Process, freeze_support
import csv
import sys
import json
import sqlite3
import os
import nltk
import pickle
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem.wordnet import WordNetLemmatizer
from gensim import corpora, models, similarities, utils
from nltk.stem.lancaster import LancasterStemmer
#st = LancasterStemmer()
all_doc_token = []
id_token = {}
if __name__ == '__main__':
    freeze_support()


with open("toy.csv", "r") as paperscsv:
    papersreader = csv.reader(paperscsv,delimiter=',',quotechar='"')
    for doc in papersreader:
        # skip the header
        # if doc_nr > 0:
        id = int(doc[0])
        text = doc[-1]
        nett_token = []
        grammar = Word(alphas)
        stops = set(stopwords.words("english"))
        tokenized_text = Word(alphas).scanString(text)
        for token in tokenized_text: # for each token, steam it, check stop words and length, add to list
            token = stem(str(token[0]).lower())
            token = token[2:-2] #['blabla']->blabla
            #print token
            if (token not in stops )and (len(token) > 1):
                nett_token.append(token)  # ['self','orgainazation',...]
        all_doc_token.append(nett_token)
        #print all_doc_token
        id_token[id] = nett_token
        #print id_token
    with open("docidtokens.txt", 'w') as f: #{"10":["mean","ddd"]....}
        json.dump(id_token, f)
    print id_token #{1:['self','organization']}
    dictionary = corpora.Dictionary(all_doc_token)
    dictionary.save('alltoken.dict')
    #print all_doc_token #[['self','organization']]
    corpus = [dictionary.doc2bow(docs) for docs in all_doc_token]
    corpora.MmCorpus.serialize('corpus.mm', corpus)
    #print corpus #[[(0,1),(1,1)]]
    tfidf = models.TfidfModel(corpus)
    corpus_tfidf = tfidf[corpus]
    #lda = models.LdaModel(corpus_tfidf, id2word=dictionary, alpha='auto', num_topics=40)
    #lda.save('model.lda')

    #number_coherence = {}
    #for i in range(20,60):
    lda = models.LdaModel(corpus=corpus, id2word=dictionary, iterations=10, num_topics=50)
    ldacm = models.CoherenceModel(model=lda, texts=all_doc_token, dictionary=dictionary, coherence='c_v')
    print ldacm.get_coherence()
        #number_coherence[i] = ldacm.get_coherence()
        #number_coherence[i] = ldacm.get_coherence()
        #print number_coherence




