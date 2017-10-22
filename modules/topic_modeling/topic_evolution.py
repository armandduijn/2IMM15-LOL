import json
import numpy as np
import matplotlib.pyplot as plt

topic_number = 8
with open('./topics/year_topicid.txt','r') as f:
    year_topic = json.load(f)
#print year_topic
year_topic_freq = {}
for year in year_topic.keys():
    topic_freq = {}
    for topic in year_topic[year]:
        #print type(topic)
        if topic not in topic_freq.keys():
            topic_freq[topic] = 0
        topic_freq[topic] += 1
    year_topic_freq[year] = topic_freq
#print year_topic_freq
####
topic_year_fre = {}
for topic in range(0,topic_number):
    year_fre = {}
    for year in year_topic_freq.keys():
        if topic in year_topic_freq[year].keys():
            year_fre[int(year)] = year_topic_freq[year][topic]
        else:
            year_fre[int(year)] = 0
    topic_year_fre[topic] = year_fre
#print topic_year_fre

with open('./topics/topic_year_frequency.txt','w') as f:
    json.dump(topic_year_fre,f)

tableau20 = [(31, 119, 180), (174, 199, 232), (255, 127, 14), (255, 187, 120),
             (44, 160, 44), (152, 223, 138), (214, 39, 40), (255, 152, 150),
             (148, 103, 189), (197, 176, 213), (140, 86, 75), (196, 156, 148),
             (227, 119, 194), (247, 182, 210), (127, 127, 127), (199, 199, 199),
             (188, 189, 34), (219, 219, 141), (23, 190, 207), (158, 218, 229)]

for i in range(len(tableau20)):
    r, g, b = tableau20[i]
    tableau20[i] = (r / 255., g / 255., b / 255.)

with open('./topics/year_doc.txt','r') as f:
    year_doc = json.load(f)

x = np.arange(1987,2017,1)
# plt.figure()
for topic in topic_year_fre:
    topic_list = []
    for dot in topic_year_fre[topic].keys():
        total_paper_dot = len(year_doc[unicode(dot)])
        total_paper_dot = float(total_paper_dot)
        topic_year_fre[topic][dot] = float(topic_year_fre[topic][dot])
        normlized_fre = topic_year_fre[topic][dot] #/ total_paper_dot
        topic_list.append(normlized_fre)
    topic_list = np.array(topic_list)
    print "topic %d" %topic
    for x in topic_list:
        plt.figure()
        plt.plot(x,topic_list,lw=2.5, color=tableau20[topic], label="TOPIC %d" % topic)
        plt.xlabel("year")
        plt.ylabel("frequency")
        plt.title("Topic evolution of Topic%d" % topic)
        plt.savefig('./topics/topic-pics/Topic evolution of Topic%d' % topic)
        # plt.show()






