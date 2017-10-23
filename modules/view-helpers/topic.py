#!/usr/bin/env python

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')))
#print os.path.dirname(__file__)
from modules.topic_modeling.mapper import TopicQuery

"""
This script takes as argument an author query string.
The result is a comma-separated list of the indices of the topics that match the query.
"""

topics = TopicQuery(sys.argv[1])
print ','.join(topics)

#print sys.argv[1]