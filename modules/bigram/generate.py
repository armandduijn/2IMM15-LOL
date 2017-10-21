from __future__ import print_function
import sys
import os
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')))
from pyparsing import *
from nltk.tokenize import sent_tokenize, word_tokenize
from nltk.corpus import stopwords
from modules.stemming.porter2 import stem
from sets import Set
import pprint
import math
import operator
import sqlite3
import functools
import string
import re

pp = pprint.PrettyPrinter(indent=2)



os.chdir(os.path.dirname(__file__))

stop = stopwords.words('english') + list(string.punctuation)

def map():

    with sqlite3.connect(os.getcwd() + '/../../data/database.sqlite') as database:
        cursor = database.cursor()
        cursor.execute('SELECT paper_text FROM papers')
        for doc in cursor.fetchall():
            text = doc[0]
            lines = sent_tokenize(text)
            for line in lines:
               words = word_tokenize(line.lower())
               words = [i for i in words if i not in stop and not any((c.isdigit() or c in string.punctuation) for c in i) and not len(i) == 1]
               for first, second in zip(words, words[1:]):
                   sFirst = stem(first)
                   print(sFirst, second, 1)
    return True

def reduce():

    from operator import itemgetter
    import sys

    current_word = None
    current_count = 0

    # input comes from STDIN
    for line in sys.stdin:
        # remove leading and trailing whitespace
        line = line.strip()

        # parse the input we got from mapper.py
        word1, word2, count = line.split()

        # convert count (currently a string) to int
        try:
            count = int(count)
        except ValueError:
            # count was not a number, so silently
            # ignore/discard this line
            continue

        # this IF-switch only works because Hadoop sorts map output
        # by key (here: word) before it is passed to the reducer
        if current_word == word1+"."+word2:
            current_count += count
        else:
            if current_word:
                # write result to STDOUT
                print(word1, word2, current_count, sep='\t')
            current_count = count
            current_word = word1+"."+word2

    # do not forget to output the last word if needed!
    if current_word == word1+"."+word2:
        print(word1, word2, current_count, sep='\t')


if __name__ == "__main__":
    # nothing
    reduce()
