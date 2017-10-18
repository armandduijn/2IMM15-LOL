import re
from pyparsing import Word, alphanums, Keyword, Group, Combine, Forward, Suppress, Optional, OneOrMore, oneOf, Literal, nums, ZeroOrMore
from modules.stemming.porter2 import stem
from modules.indexer.indexer import GetWord, GetNot, GetQuotesExact, GetWordWildcard, GetWord, Search, GetPapersBy, GetPapersIn
from sets import Set

def execute(container, query):
    return Query(query, container)

def Syntax():
    operatorOr = Forward()

    operatorWord = Group(Combine(Word(alphanums) + Suppress('*'))).setResultsName('wordwildcard') | \
                   Group(Word(alphanums)).setResultsName('word')

    operatorQuotesContent = Forward()
    operatorQuotesContent << (
        (operatorWord + operatorQuotesContent) | operatorWord
    )

    operatorQuotes = Group(
        Suppress('"') + operatorQuotesContent + Suppress('"')
    ).setResultsName("quotes") | operatorWord

    operatorParenthesis = Group(
        (Suppress("(") + operatorOr + Suppress(")"))
    ).setResultsName("parenthesis") | operatorQuotes

    operatorNot = Forward()
    operatorNot << (Group(
        Suppress(Keyword("not", caseless=True)) + operatorNot
    ).setResultsName("not") | operatorParenthesis)

    operatorAnd = Forward()
    operatorAnd << (Group(
        operatorNot + Suppress(Keyword("and", caseless=True)) + operatorAnd
    ).setResultsName("and") | Group(
        operatorNot + OneOrMore(~oneOf("and or") + operatorAnd)
    ).setResultsName("and") | operatorNot)

    operatorOr << (Group(
        operatorAnd + Suppress(Keyword("or", caseless=True)) + operatorOr
    ).setResultsName("or") | operatorAnd)

    return operatorOr

def evaluateAnd(argument):
    return evaluate(argument[0]).intersection(evaluate(argument[1]))

def evaluateOr(argument):
    return evaluate(argument[0]).union(evaluate(argument[1]))

def evaluateNot(argument):
    return GetNot(evaluate(argument[0]))

def evaluateParenthesis(argument):
    return evaluate(argument[0])

def evaluateQuotes(argument):
    """Evaluate quoted strings

    First is does an 'and' on the indidual search terms, then it asks the
    function GetQuoted to only return the subset of ID's that contain the
    literal string.
    """
    r = Set()
    search_terms = []
    for item in argument:
        search_terms.append(item[0])
        if len(r) == 0:
            r = evaluate(item)
        else:
            r = r.intersection(evaluate(item))
    return GetQuotesExact(' '.join(search_terms))

def evaluateWord(argument):
    return GetWord(argument[0])

def evaluateWordWildcard(argument):
    return GetWordWildcard(argument[0])

def evaluate(argument):
    methods = {
        'and': evaluateAnd,
        'or': evaluateOr,
        'not': evaluateNot,
        'parenthesis': evaluateParenthesis,
        'quotes': evaluateQuotes,
        'word': evaluateWord,
        'wordwildcard': evaluateWordWildcard,
    }

    return methods[argument.getName()](argument)


def stemWord(matchobj):
     # if necessary ignore keywords in the stemming, but the Porter stemmer doesn't affect them.
     return stem(matchobj.group(0))

def stemQueryString(queryString):
    queryString = re.sub(r'([a-zA-Z]*)', stemWord, queryString)
    return queryString

def Query(query, container):
    """
    Returns a set of documents from the index for a given boolean query.
    Or return
    """
    if query.startswith("author:"):
        authorid = query[7:]
        return GetPapersBy(authorid)
    elif query.startswith("year:"):
        year = query[5:]
        return GetPapersIn(year)
    elif " or " in query.lower() or " and " in query.lower() or " not " in query.lower():
        expr = Syntax()
        return evaluate(expr.parseString(stemQueryString(query))[0])
    else:
        return Search(query)

def PrintQueryTree(query):
    expr = Syntax()
    results = expr.parseString(stemQueryString(query))
    return results.dump()

if __name__ == "__main__":
    documents = Query("author:7477")
