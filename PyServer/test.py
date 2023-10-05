import os

import openai
from flask import Flask, request

app = Flask(__name__)
openai.api_key = os.getenv("OPENAI_API_KEY")


@app.route("/", methods=("GET","POST"))
def index():
    if request.method == "POST":
        req_str=request.get_data().decode("utf-8")
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "You are an assistant that receives an English word and returns an example sentence and its Japanese translation."},
                {"role": "system", "content": "The answer content will be 'English: example sentence' and 'Japanese: example sentence'."},
                {"role": "user", "content": req_str},
            ]
        )
        return response['choices'][0]['message']['content']
    return "<html><body><h1>Please POST access.</h1></body></html>"
if __name__ == "__main__":
    app.run(port=8888)