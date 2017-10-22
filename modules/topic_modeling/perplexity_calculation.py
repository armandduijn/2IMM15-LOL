## this is used to calculate the perplexity of models with different topic numbers in it
import pandas as pd
import numpy
import math
import string
import matplotlib.pyplot as plt
import gensim
import json

#####input file  = dictionary and corpus from tryabstract.py#####

dictionary = gensim.corpora.Dictionary.load('./topics/alltoken.dict')
corpus = gensim.corpora.MmCorpus('./topics/corpus.mm')
tfidf = gensim.models.TfidfModel(corpus)
corpus_tfidf = tfidf[corpus]

#####generate the models and calculate each perplexity#####
##### first check a wild range, find the break point and then zoom in ######
grid = {}
parameter_list = range(5, 45, 5)
for i in parameter_list:
    lda = gensim.models.LdaModel(corpus=corpus_tfidf, id2word=dictionary, iterations=5, num_topics=i)
    grid[i] = []
    perplex = lda.log_perplexity(corpus_tfidf, total_docs=len(corpus_tfidf))
    grid[i].append(perplex)

#####plot the perplexity-topic numbers graph#####

df = pd.DataFrame(grid)
ax = plt.figure(figsize=(7, 4), dpi=300).add_subplot(111)
df.iloc[0].transpose().plot(ax=ax,  color="#4682B4")
plt.xlim(parameter_list[0], parameter_list[-1])
plt.ylabel('Perplexity')
plt.xlabel('topics')
plt.savefig('./topics/perplexity-topicnumbers.png')
plt.show()