from graph import Vertex, Graph

import pickle
import csv
from scipy import sparse
from sklearn.metrics.pairwise import cosine_similarity

MODULE_NAME = 'similarity'

def register(container):
    ''' Registers data to the static container '''

    path = container['data_dir'] + '/derived/similarity.lol'

    try:
        container[MODULE_NAME] = pickle.load(open(path, 'rb'))
    except Exception, e:
        container[MODULE_NAME] = dict()

def graph(g):
    if isinstance(g, Graph):
        return str(g.adjacency_list()) + '\n' + '\n' + str(g.matrix())

def execute(container, author_id):
    if MODULE_NAME in container:
        ranked_similarity = container[MODULE_NAME]

        if author_id in ranked_similarity:
            return ranked_similarity[author_id]

        return []
    else:
        with open(container['data_dir'] + '/collaboration-graph.csv') as csvfile:
            # print "Reading CSV..."

            readcsv = csv.reader(csvfile, delimiter=',')

            graph = Graph()

            for row in readcsv:
                vertex_from = graph.get_vertex(row[0])
                vertex_to = graph.get_vertex(row[2])

                graph.add_edge(vertex_from, vertex_to)

            # print "Finished reading CSV"
            # print "Calculating similarities..."

            matrix = sparse.csr_matrix(graph.matrix())
            similarities = cosine_similarity(matrix, dense_output=True)

            # print "Finished calculating similarities"

            ranked_similarities = {}

            indices = graph.get_indices()
            indices2 = graph.get_indices(True)

            # print "Sorting results..."

            for i in range(0, len(similarities)):
                similarity = zip(sorted(indices), similarities[i])

                similarity.sort(key=lambda x: x[1], reverse=True)

                ranked_similarities[indices2[i]] = similarity[0:50]

            pickle.dump(ranked_similarities, open(container['data_dir'] + '/similarity.lol', 'wb'), protocol=pickle.HIGHEST_PROTOCOL)

            print "Finished. Houdoe!"

            return ranked_similarities[author_id]
