from __future__ import print_function
from colorprint import *
import pickle
import os
import imp
import sys

def main():
    container = {}

    # Store absolute path of data files
    container['data_dir'] = os.path.abspath(os.path.join(os.getcwd(), '..', 'data'))

    directories = filter(lambda x: os.path.isdir(x), os.listdir(os.getcwd()))

    for directory in directories:
        directory = os.path.abspath(directory)

        if os.path.exists(directory + "/main.py"):
            sys.path.append(directory)

            module = imp.load_source('module.name', directory + "/main.py")

            if hasattr(module, 'register'):
                if hasattr(module, 'MODULE_NAME'):
                    print("Importing '%s'..." % module.MODULE_NAME, color='green')

                module.register(container);
            else:
                print("No register() function found in '%s/main.py'" % directory, color='yellow')

    return container
