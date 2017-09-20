from pyparsing import Word, alphanums, Keyword, Group, Combine, Forward, Suppress, Optional, OneOrMore, oneOf, Literal, nums, ZeroOrMore
from pprint import pprint
from var_dump import var_dump
import json

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

if __name__ == "__main__":
    expr = Syntax()
    def test(s):
        results = expr.parseString( s )
        print s+'\n->\n'+results.dump()

    #test( "(9 AND 3)" )
    test( "a or b or b and (not d or (not e and f) and g)" )
