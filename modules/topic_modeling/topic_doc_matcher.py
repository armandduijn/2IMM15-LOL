#####this is for calculating the LDA model using the dictionary and the corpus from tryabstract.py and match topic-document####
####input file: dictionary , corpus
####output file: lda model file, topic_wordlist.txt,  docidtokens.txt , docid_topicid.txt,   topicid_docid.txt'
import gensim
import json
import warnings
warnings.filterwarnings("ignore")
from pyparsing import *
from modules.stemming.porter2 import stem
import csv
import sys
import pickle
from nltk.corpus import stopwords

##### downloading the dictionary and corpus#####
number_of_topics = 10 ## this is a number verified by perplexity
dictionary = gensim.corpora.Dictionary.load('./topics/alltoken.dict')
corpus = gensim.corpora.MmCorpus('./topics/corpus.mm')

tfidf = gensim.models.TfidfModel(corpus)
corpus_tfidf = tfidf[corpus]
model = gensim.models.LdaModel(corpus=corpus_tfidf, id2word=dictionary, iterations=100, num_topics=number_of_topics)  # try with title, no ptfidf
model.save('./topics/lda_topic%d.lda' % number_of_topics)

###getting the word id and the probabilities####
topic_words = {}
words_per_topic = 10
for topic in range(0, number_of_topics):
    # save top 10 words for one certain topic
    topic_words[topic] = model.get_topic_terms(topic, words_per_topic)

####getting the id2token mapping so we can know the actual words
topic_wordlist = {}
topic_id = -1
for topic in topic_words.keys():
    topic_id += 1
    wordlist = {}
    tupleArray = topic_words[topic]
    for wordid, score in tupleArray:
        word = model.id2word.id2token[wordid]
        wordlist[word] = score
    topic_wordlist[topic_id] = wordlist

##save the dictionary(dictionary containing the topic:{word:pr})
with open('./topics/topic_wordlist.txt','w') as f:
    json.dump(topic_wordlist,f)



# # count = 0
# # for key in dictionary:
# #     count += len(key)
# print dictionary
# print max(dictionary.keys())

#merge an empty dictionary with the old dictionary to give the query tokens same id
id2word = gensim.corpora.Dictionary()
_ = id2word.merge_with(dictionary)

docidtotopics = {}
topics_per_doctument = 1

##for each paper in the database, we calculate the probabillities of that belongs to certain topic
##so we chose the most relevant one, and save it
##which give us the relations between the documents and topics
with open('./topics/docidtokens.txt','r') as f:
    query_dictionary = json.load(f)
    for key in query_dictionary.keys():
        topic = []
        doc_id = int(key)
        query = query_dictionary[key]
        query = id2word.doc2bow(query)
        a = list(sorted(model[query], key=lambda x: -x[1]))#a[0] = (24, 0.010154742173743013)
        #model.print_topic(a[0][0])
        for i in range(0,topics_per_doctument):
            topic.append(a[i][0])
        docidtotopics[doc_id] = topic #{"12":[1,2,3,4,5],..}
    with open('./topics/docid_topicid.txt','w') as f:
        json.dump(docidtotopics,f)

    #print "docidtotopics = "
    #print docidtotopics

    #TODO:convert doc_to_topic to topic_to_doc

    topicidtodocs = {}

    #TODO:connect the number_of_topics to former py, make sure they change together, and find a proper value

    for i in range(number_of_topics):
        topicidtodocs[i] = []
    for key in docidtotopics.keys():
        doc_id = int(key)
        for item in docidtotopics[key]:
            #print docidtotopics[key] #[37, 14, 32, 22, 21]
            topicidtodocs[item].append(doc_id)
    #print topicidtodocs #{0: [1025, 1031, 103]...}
    with open('./topics/topicid_docid.txt', 'w') as f:
        json.dump(topicidtodocs, f)





#"""


