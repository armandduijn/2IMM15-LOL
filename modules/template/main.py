MODULE_NAME = 'template'

def register(container):
    container[MODULE_NAME] = 'Foobar'

def execute(container, argument):
    if 'data_dir' in container:
        # Do something with container

        print container['data_dir']

    return "Argument: " + argument
