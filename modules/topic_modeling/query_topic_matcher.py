### This is for query--topic matching ,
##input: query,,,,
##output: topic id
import gensim
import string
import json
import nltk
# nltk.download('wordnet')
from nltk.stem.wordnet import WordNetLemmatizer
lm = WordNetLemmatizer()

#TODO: PHP stuff!? query send to here!!!!!
# TODO: it should be a raw string  because I  need to lemmatize it instead of stem it
query = 'machine learning'
# print "query: " + query
# print "searching for documents in the same topic..."
query = query.split(' ')
for word in query:
    word = lm.lemmatize(word)


lda = gensim.models.LdaModel.load('./topics/lda_topic8.lda')
dictionary = gensim.corpora.Dictionary.load('./topics/alltoken.dict')

id2word = gensim.corpora.Dictionary()
_ = id2word.merge_with(dictionary)

query = id2word.doc2bow(query)
a = list(sorted(lda[query], key=lambda x: -x[1]))
most_matched_topic = a[0][0]


#####here is output####


most_matched_topic = str(most_matched_topic)
print most_matched_topic
