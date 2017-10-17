def execute(container, argument):
    if 'name' in container:
        # Do something with container

        print container['name']

    return "Argument: " + argument
