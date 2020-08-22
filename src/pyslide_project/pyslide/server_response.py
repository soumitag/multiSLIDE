import json


class StandardResponse:
    def __init__(self, status, message='', detailed_message=''):
        self.status = status
        self.message = message
        self.detailed_message = detailed_message

    def as_json(self):
        return json.dumps(self.__dict__)
