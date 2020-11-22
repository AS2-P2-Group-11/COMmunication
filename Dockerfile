FROM tiangolo/uvicorn-gunicorn:python3.8
COPY ./requirement.txt /app/requirement.txt
RUN cd /app && pip3 install -r requirement.txt
WORKDIR /app/backend

CMD uvicorn main:app --reload --port 9000
