from flask import Flask, abort, request, jsonify
import os
import sys
import bootstrap

# Load app
app = Flask(__name__)

# Load dependencies
container = bootstrap.main()

@app.route("/")
def hello():
    module_name = request.args.get('module', default='', type=str)
    argument = request.args.get('argument', default='', type=str)

    if module_name == '':
        return abort(400)

    module_dir = get_path(module_name)

    if os.path.isdir(module_dir):
        sys.path.append(module_dir)

        main = __import__('main')
        output =  main.execute(container, argument)

        if type(output) is dict or type(output) is list:
            return jsonify(output)

        return output
    else:
        return abort(400)

def get_path(filename):
    cwd = os.path.dirname(__file__)

    return os.path.abspath(os.path.join(cwd, '..', 'modules', filename))


