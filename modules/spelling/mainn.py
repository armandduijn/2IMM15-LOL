import os
import pickle

from collections import Counter
from nltk.tokenize import word_tokenize


'''
Rename me to main.py, otherwise import errors
'''

def words(text):
    return word_tokenize(text.lower())

def register(container):
    path = container['data_dir'] + '/papers.csv'

    print "Starting"
    counter = Counter(words(open(path).read()))
    print "Dumping"
    pickle.dump(counter, open(container['data_dir'] + '/spelling.lol', 'wb'), protocol=pickle.HIGHEST_PROTOCOL)

register({
    'data_dir': os.path.abspath(os.path.join(os.getcwd(), '..', '..', 'data'))
})
