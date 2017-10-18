import pickle
import sys
import os
import pyparsing

MODULE_NAME = 'indexer'

def register(container):
    ''' Registers data to the static container '''

    path = container['data_dir'] + '/derived/'

    try:
        collection = pickle.load(open(path + "index.lol", "rb"))
        idf = pickle.load(open(path + "idf.lol", "rb"))
        docs = pickle.load(open(path + "doc_length.lol", "rb"))
        container[MODULE_NAME] = [collection, idf, docs]
    except Exception, e:
        container[MODULE_NAME] = dict()

def execute(container, dataType):
    if MODULE_NAME in container:
        array = container[MODULE_NAME]

        if str(dataType).startswith("index"):
            return array
        elif dataType == "idf":
            return array[1]
        elif dataType == "doc_length":
            return array[2]
    else:
        collection = defaultdict(functools.partial(defaultdict, list))
        doc_nr = 0
        idf = {}
        docs  = {}

        print "Building indexer..."

        #with open(file_dataset, "r") as paperscsv:
            #papersreader = csv.reader(paperscsv,delimiter=',')
        with sqlite3.connect(os.getcwd() + '/../../data/database.sqlite') as database:
            cursor = database.cursor()
            cursor.execute('SELECT * FROM papers')
            for doc in cursor.fetchall():
            #for doc in papersreader:
                # skip the header
                #if doc_nr > 0:
                id = doc[id_col]
                text = doc[text_col]
                # extract tokens from the title and the text
                token_pos = 0
                print "Examining doc: " + str(id)
                tokenized_text = Word(alphas).searchString(text)
                docs[id] = len(tokenized_text)
                for token in tokenized_text:
                    token = stem(str(token[0]).lower())
                    token_pos += 1
                    collection[token][id].append(token_pos)
                doc_nr += 1

        print "Calculating idf..."

        for term in collection.keys():
            idf[term] = math.log10(doc_nr/float(len(collection[term])))

        path = container['data_dir'] + '/derived/'

        print "Dumping index..."

        pickle.dump(collection,open(path + "index.lol", "wb"))

        print "Dumping idf..."

        pickle.dump(idf, open(path + "idf.lol", "wb"))

        print "Dumping doc_length..."

        pickle.dump(docs, open(path + "doc_length.lol", "wb"))

        if dataType == "index":
            return collection
        elif dataType == "idf":
            return idf
        elif dataType == "doc_length":
            return docs

    return []
