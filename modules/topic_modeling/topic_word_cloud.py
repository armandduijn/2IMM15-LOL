import matplotlib.pyplot as plt
import gensim
from wordcloud import WordCloud


tableau20 = [(31, 119, 180), (174, 199, 232), (255, 127, 14), (255, 187, 120),
             (44, 160, 44), (152, 223, 138), (214, 39, 40), (255, 152, 150),
             (148, 103, 189), (197, 176, 213), (140, 86, 75), (196, 156, 148),
             (227, 119, 194), (247, 182, 210), (127, 127, 127), (199, 199, 199),
             (188, 189, 34), (219, 219, 141), (23, 190, 207), (158, 218, 229)]

for i in range(len(tableau20)):
    r, g, b = tableau20[i]
    tableau20[i] = (r / 255., g / 255., b / 255.)

lda = gensim.models.LdaModel.load('./topics/lda_topic8.lda')

for t in range(lda.num_topics):
    # plt.figure()
    plt.imshow(WordCloud(background_color='white',width=1200, height=1200).fit_words(dict(lda.show_topic(t, 15))))
    plt.axis("off")
    plt.title("Topic #" + str(t))
    # plt.show()
    plt.savefig('./topics/topic-pics/wc-topic%d.png' % t)