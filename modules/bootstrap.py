import pickle
import os

def main():
    container = {}

    # Load author similarities
    container['ranked_similarity'] = load_dict('derived/similarity.lol')

    return container

def load_dict(filename):
    print "Importing '%s'..." % filename

    path = get_path(filename)

    try:
        return pickle.load(open(path, 'rb'))
    except Exception, e:
        print str(e)

        return dict()

def get_path(filename):
    cwd = os.path.dirname(__file__)

    return os.path.abspath(os.path.join(cwd, '..', 'data', filename))

