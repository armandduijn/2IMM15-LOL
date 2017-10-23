###this is for dynamic topic modeling####
import gensim
import json

from gensim.corpora import Dictionary, bleicorpus
import numpy
from gensim.matutils import hellinger

my_corpus = gensim.corpora.MmCorpus('../topic_modeling/topics/corpus.mm')
dictionary = gensim.corpora.Dictionary.load('../topic_modeling/topics/alltoken.dict')
my_timeslices = [90,94,101,143,144,127,158,140,152,152,150,151,150,152,197,207,198,207,207,204,217,250,262,292,306,368,360,411,403,567]
# my_timeslices = [184,244,271,298,304,301,302,404,405,411,467,554,674,771,970]

ldaseq = gensim.models.ldaseqmodel.LdaSeqModel(corpus=my_corpus, id2word=dictionary, time_slice=my_timeslices, num_topics=8)
ldaseq.save('ldaseq')
# print "print topics"
# for i in range(15):#30
#     print ldaseq.print_topics(time=i,top_terms=15)
# print "topic evolution"
# for i in range(8):
#     print ldaseq.print_topic_times(topic=i)
# print "done!"

# another way from python wrapper
# dtmpath = "./dtm_release-0.8/dtm_release/dtm/main"
# dtmmodel = gensim.models.wrappers.DtmModel(dtmpath, my_corpus, my_timeslices, num_topics=8, id2word=dictionary)
# dtmmodel.save('dtm_papers')
# print dtmmodel.print_topics(num_topics=8,num_words=15,times=15)# individual list contains a tuple of the most probable words in the topic
