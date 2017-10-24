import json
import os
import operator
import csv

def TopicQuery(param):
    result = []
    script_dir = os.path.dirname(__file__)
    if param.startswith("author:"):
        # load the json of author-topic
        with open(os.path.join(script_dir, 'topic_modeling/topics/authorid_topics.txt'), 'r') as b:
            authorid = unicode(param[7:])
            map = json.load(b)
            for topicid in map[authorid]:
				result.append(str(topicid))
    elif param.startswith("year:"):
        # get the top 5 topics of this year
        with open(os.path.join(script_dir, 'topics/year_topicid.txt'), 'r') as b:
            year = unicode(param[5:])
            map = json.load(b)
            topics = {}
            for topicid in map[year]:
                if topics.has_key(topicid):
                    topics[topicid] += 1
                else:
                    topics[topicid] = 1
            sorted_topic_counts = sorted(topics.items(), key=operator.itemgetter(1), reverse=True)
            for topic,count in sorted_topic_counts:
                if len(result) == 5:
                    break
                result.append(str(topic))
    elif param.startswith("topic:"):
        # get the top 5 authors of this topic
        with open(os.path.join(script_dir, 'topics/topicid_authors.txt'), 'r') as b:
            topicid = unicode(param[6:])
            map = json.load(b)
            authors_count = {}
            # get the count of works in the topic
            for authorid in map[topicid]:
                if authors_count.has_key(authorid):
                    authors_count[authorid] += 1
                else:
                    authors_count[authorid] = 1
            # get the page rank, and combine this into the count
            with open("../../data/derived/pagerank_score.txt", "r") as pagerankfile:
                reader = csv.reader(pagerankfile, delimiter=' ')
                count = 0
                for row in reader:
                    id = row[0]
                    score = float(row[1])
                    if authors_count.has_key(id):
                        authors_count[id] = (0.5 * float(authors_count[id])) + (0.5 * score)
                        count += 1
                    if count >= len(authors_count.items()):
                        break
            sorted_authors = sorted(authors_count.items(), key=operator.itemgetter(1), reverse=True)
            for authid,count in sorted_authors:
                if len(result) == 5:
                    break
                result.append(str(authid))
    else:
		# param is topicid and we need to get documents in this topic
		with open(os.path.join(script_dir, 'topics/topicid_docid.txt'), 'r') as b:
			topicid = unicode(param)
			map = json.load(b)
			for docid in map[topicid]:
				result.append(str(docid))
    return result
