#!/bin/bash
# usage: shots.sh T out.png id1 id2 ...
T=$1; OUT=$2; shift 2
cd /home/user/Theme/tools && T=$T node shot.js x "$@" && python3 - "$OUT" <<'PY'
import json,base64,io,sys
from PIL import Image
shots=json.load(open('/tmp/shots.json'))
ims=[Image.open(io.BytesIO(base64.b64decode(u.split(',')[1]))).convert('RGB') for _,u in shots]
h=720;ims=[im.resize((int(im.width*h/im.height),h)) for im in ims]
s=Image.new('RGB',(sum(i.width for i in ims),h));x=0
for im in ims:s.paste(im,(x,0));x+=im.width
s.save(sys.argv[1]);print(s.size)
PY
