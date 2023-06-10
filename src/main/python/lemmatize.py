import sys
sys.path.append(sys.argv[1])

from iwnlp.iwnlp_wrapper import IWNLPWrapper

lemmatizer = IWNLPWrapper(lemmatizer_path=sys.argv[1]+'/IWNLP.Lemmatizer_20181001.json')

while True:
    line = input()
    try:
        word,pos = line.split(',')
    except:
        print("Failed to unpack: " + line)
    res = lemmatizer.lemmatize(word, pos_universal_google=pos)
    if res is None:
        res = lemmatizer.lemmatize_plain(word, ignore_case=False)
        if res is not None:
            print(res[0])
        else:
            print('')
    else:
        print(res[0])