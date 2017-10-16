import numpy as np

class Vertex:
    def __init__(self, name):
        self.name = name
        self.neighbours = []

    def add_neighbour(self, neighbour, undirected=True):
        if isinstance(neighbour, Vertex) and (neighbour.name not in self.neighbours):
            self.neighbours.append(neighbour.name)
            self.neighbours = sorted(self.neighbours)

            # Add an edge in the opposite direction if the graph is undirected
            if undirected:
                neighbour.neighbours.append(self.name)
                neighbour.neighbours = sorted(neighbour.neighbours)

    def add_neighbours(self, neighbours, undirected=True):
        for neighbour in neighbours:
            self.add_neighbour(neighbour, undirected)

    def __repr__(self):
        return str(self.neighbours)

class Graph:
    def __init__(self):
        self.vertices = {}

    def get_vertex(self, name):
        if name in self.vertices.keys():
            vertex = Vertex(name)

            for neighbour in self.vertices[name]:
                vertex_neighbour = Vertex(neighbour)

                vertex.add_neighbour(vertex_neighbour)

            return vertex
        else:
            return Vertex(name)

    def add_vertex(self, vertex):
        if isinstance(vertex, Vertex):
            self.vertices[vertex.name] = vertex.neighbours

    def add_vertices(self, vertices):
        for vertex in vertices:
            self.add_vertex(vertex)

    def add_edge(self, vertex, neighbour, undirected=True):
        if isinstance(vertex, Vertex) and isinstance(neighbour, Vertex):
            vertex.add_neighbour(neighbour, undirected)

            self.vertices[vertex.name] = vertex.neighbours
            self.vertices[neighbour.name] = neighbour.neighbours

    def add_edges(self, edges, undirected=True):
        for edge in edges:
            self.add_edge(edge[0], edge[1], undirected)

    def get_vertices(self):
        return sorted(self.vertices.keys())

    def get_indices(self, reversed=False):
        vertices = self.get_vertices()
        indices = dict(zip(vertices, range(len(vertices))))

        if reversed:
            return {v: k for k, v in indices.items()}

        return indices

    def adjacency_list(self):
        if len(self.vertices) >= 1:
            return [str(key) + ":" + str(self.vertices[key]) for key in self.get_vertices()]

    def matrix(self):
        if len(self.vertices) >= 1:
            matrix = np.zeros(shape=(len(self.vertices), len(self.vertices)))

            names = self.get_vertices()
            indices = self.get_indices()

            for i in range(len(names)):
                for j in range(i, len(self.vertices)):
                    for el in self.vertices[names[i]]:
                        j = indices[el]

                        matrix[i, j] = 1

            return matrix
