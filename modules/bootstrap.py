from __future__ import print_function
from colorprint import *
import pickle
import os
import imp
import sys
import importlib

def main():
    container = {}

    # Store absolute path of data files
    container['data_dir'] = os.path.abspath(os.path.join(os.getcwd(), '..', 'data'))

    directories = filter(lambda x: os.path.isdir(x), os.listdir(os.getcwd()))

    # Keep a reference
    cwd = os.getcwd()

    for directory in directories:
        full_path = os.path.abspath(directory)

        os.chdir(cwd)

        if os.path.exists(full_path + "/main.py"):
            module = importlib.import_module('modules.' + directory + '.main')

            if hasattr(module, 'register'):
                if hasattr(module, 'MODULE_NAME'):
                    print("Importing '%s'..." % module.MODULE_NAME, color="green")
                else:
                    print("Importing '%s'..." % directory, color="green")

                module.register(container);
            else:
                print("No register() function found in '%s/main.py'" % full_path, color="yellow")

    return container
