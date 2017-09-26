from pyparsing import Word, alphanums, Keyword, Group, Combine, Forward, Suppress, Optional, OneOrMore, oneOf, Literal, nums, ZeroOrMore
from pprint import pprint
from var_dump import var_dump
from modules.stemming.porter2 import stem
import json
import re

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

def stemWord(matchobj):
     # if necessary ignore keywords in the stemming, but the Porter stemmer doesn't affect them.
     return stem(matchobj.group(0))

def stemQueryString(queryString):
    queryString = re.sub(r'([a-zA-Z]*)', stemWord, queryString)
    return queryString

if __name__ == "__main__":
    expr = Syntax()
    def test(s):
        results = expr.parseString( stemQueryString(s) )
        print s+'\n->\n'+results.dump()

    #test( "(9 AND 3)" )
    test( "data or usage or mining and (not porter or (not retrieval and qualified) and greedy)" )
