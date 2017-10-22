##input : alltokens.txt,,,corpus,,,dictionary

import gensim
import json
from multiprocessing import Process, freeze_support
with open('./topics/alltokens.txt','r') as f:
    texts = json.load(f)

corpus = gensim.corpora.MmCorpus('./topics/corpus.mm')
dictionary = gensim.corpora.Dictionary.load('./topics/alltoken.dict')

coherence = {}
for number in range(5,10,1):
    lda = gensim.models.LdaModel(corpus=corpus, id2word=dictionary, iterations=5, num_topics=number)
    cm = gensim.models.CoherenceModel(model=lda, texts=texts, dictionary=dictionary, coherence='c_v')
    if __name__ == '__main__':
        freeze_support()
        print number
        print cm.get_coherence()

