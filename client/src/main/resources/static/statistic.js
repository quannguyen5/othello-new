// detail
const statisticDetail = (playerId) => {

    window.location.href = `/statistic-detail/${playerId}`;
}

// result type
const getTypeResult = (currentUserId, memberId, type) => {
    window.location.href = `/type-result-detail?currentUserId=${currentUserId}&memberId=${memberId}&type=${type}`;
}