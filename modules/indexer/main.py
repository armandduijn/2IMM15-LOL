import pickle
from indexer import Index

MODULE_NAME = 'indexer'

def register(container):
    ''' Registers data to the static container '''
    try:
        container[MODULE_NAME] = Index(file_dump=container['data_dir'] + '/derived/')
    except Exception, e:
        container[MODULE_NAME] = dict()

def execute(container, dataType):
    if MODULE_NAME in container:
        array = container[MODULE_NAME]
    else:
        array = Index(file_dump=container['data_dir'] + '/derived/')

    if dataType == "index":
        return array[0]
    elif dataType == "idf":
        return array[1]
    elif dataType == "doc_length":
        return array[2]

    return []
