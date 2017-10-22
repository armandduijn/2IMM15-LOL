import gensim
import string
import json
import nltk
nltk.download('wordnet')
from nltk.stem.wordnet import WordNetLemmatizer
from gensim import corpora, models, similarities, utils
from nltk.stem.lancaster import LancasterStemmer
st = LancasterStemmer()
lm = WordNetLemmatizer()

query = 'lda'
print "query: " + query
print "searching for documents in the same topic..."
query = query.split(' ')
for word in query:
    word = lm.lemmatize(word)


with open('./topics/topicid_docid.txt','r') as f:
    topicid_docid = json.load(f)

lda = gensim.models.LdaModel.load('./topics/lda_topic8.lda')
dictionary = gensim.corpora.Dictionary.load('./topics/alltoken.dict')


id2word = gensim.corpora.Dictionary()
_ = id2word.merge_with(dictionary)

query = id2word.doc2bow(query)
a = list(sorted(lda[query], key=lambda x: -x[1]))
most_matched_topic = a[0][0]
print "topic" + str(most_matched_topic)
print topicid_docid[str(most_matched_topic)]