import sys
sys.path.append('/home/florian/git/IWNLP-py')

from iwnlp.iwnlp_wrapper import IWNLPWrapper

lemmatizer = IWNLPWrapper(lemmatizer_path=sys.argv[1])

while True:
    word,pos = input().split(',')
    res = lemmatizer.lemmatize(word, pos_universal_google=pos)
    if res is None:
        res = lemmatizer.lemmatize_plain(word, ignore_case=False)
        if res is not None:
            print(res[0])
        else:
            print('')
    else:
        print(res[0])