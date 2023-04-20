from flask import Flask, Response, request
from flask_cors import CORS
import json


app = Flask(__name__)
CORS(app, resources={r"*": {"origins": "*"}})


class UsernameExistsException(Exception):
    pass


class TokenNotValidException(Exception):
    pass


class Users:
    users = {"admin": "admin"}

    @classmethod
    def create(cls, username, password):
        if username in cls.users:
            raise UsernameExistsException()
        cls.users[username] = password

    @classmethod
    def check(cls, username, password):
        return username in cls.users and cls.users[username] == password


class Token:
    @staticmethod
    def getToken(username, password):
        return username + "|" + password

    @staticmethod
    def getData(token, bearer=False):
        if bearer == True:
            if token[0:7] != "Bearer ":
                raise TokenNotValidException()
            token = token[7:]
        try:
            return token[:token.index("|")], token[token.index("|")+1:]
        except ValueError:
            raise TokenNotValidException()


@app.route("/")
def hello_world():
    return "<p>Hello, World!</p>"

@app.route("/api/v3/auth/signup", methods=["POST"])
def signUp():
    request_data = request.get_json()
    username, password = request_data["username"], request_data["password"]
    resp = Response()
    try:
        Users.create(username, password)
        resp.set_data("")
        resp.status = 201
    except UsernameExistsException:
        resp.set_data("Wrong password")
        resp.status = 400
    resp.headers['Content-Type'] = 'text/plain'
    return resp

@app.route("/api/v3/auth/signin", methods=["POST"])
def signIn():
    request_data = request.get_json()
    username, password = request_data["username"], request_data["password"]
    resp = Response()
    if not Users.check(username, password):
        resp.set_data("")
        resp.status = 403
    else:
        token = Token.getToken(username, password)
        resp.set_data(json.dumps({'token': token}))
        resp.status = 200
    resp.headers['Content-Type'] = 'application/json'
    return resp

@app.route("/api/v3/route/find", methods=["GET"])
def findRoute():
    # process request
    token = request.headers.get("Authorization", "")
    resp = Response()
    try:
        username, password = Token.getData(token, bearer=True)
        if not Users.check(username, password):
            raise TokenNotValidException()
        ids = request.args.getlist("ids")
        resp.set_data(json.dumps({'points': [{"id": x, "name": username + "_point_" + x} for x in ids]}))
        resp.status = 200
    except TokenNotValidException:
        resp.set_data("")
        resp.status = 403
    resp.headers['Content-Type'] = 'application/json'
    return resp

if __name__ == '__main__':
    app.run(host="localhost", port=8000, debug=True)
