#!/usr/bin/env python

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')))
#print os.path.dirname(__file__)
from modules.stemming.porter2 import stem
import re

"""
This script takes as argument a string .
The result is a comma-separated list of the indices of the documents that match the query.
"""

def stemWord(matchobj):
     # if necessary ignore keywords in the stemming, but the Porter stemmer doesn't affect them.
     return stem(matchobj.group(0))

def stemString(queryString):
    queryString = re.sub(r'([a-zA-Z]*)', stemWord, queryString)
    return queryString

stemmed = stemString(sys.argv[1])
print stemmed