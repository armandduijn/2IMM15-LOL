#!/usr/bin/env python

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')))
#print os.path.dirname(__file__)
from modules.query.query import Query

"""
This script takes as argument a query string with possibly boolean expressions.
The result is a comma-separated list of the indices of the documents that match the query.
"""

documents = Query(sys.argv[1])
print ','.join(str(x) for x in documents)
