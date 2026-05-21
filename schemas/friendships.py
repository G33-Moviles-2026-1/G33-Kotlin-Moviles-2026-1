"""Friendship API schemas — include friend activity status for mobile cards."""

from pydantic import BaseModel, Field


class FriendItem(BaseModel):
    email: str
    username: str
    status: str = Field(
        default="free",
        description="Friend's activity status (incognito, busy, exercising, free, hanging_out, at_home, lunching).",
    )


class MyFriendsResponse(BaseModel):
    total: int
    items: list[FriendItem]


class CreateFriendshipRequest(BaseModel):
    correo_amigo_2: str


class AcceptFriendshipRequest(BaseModel):
    correo_amigo_1: str


class FriendshipCreatedResponse(BaseModel):
    correo_amigo_1: str
    correo_amigo_2: str
    estado: int = Field(description="0 = pending, 1 = accepted")
