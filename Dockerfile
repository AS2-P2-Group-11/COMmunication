FROM tiangolo/uvicorn-gunicorn:python3.8
COPY . /app
RUN chmod 777 /app/*
RUN cd /app && pip3 install -r requirement.txt
WORKDIR /app/backend

#CMD uvicorn main:app --reload --port 9000
