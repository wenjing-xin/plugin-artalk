;
(function (){
    let commentLinkScan = function (){
        this.linkReplace();
    }

    commentLinkScan.prototype.linkReplace = async function () {
        let whiteListDatas = await fetchWhiteList()
        let urlDomainReg = /[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\.?/;
        window.artalkItem.on("comment-rendered", ()=>{
            let aTagLists = document.querySelectorAll("#artalk-comment a");
            if(aTagLists.length){
                aTagLists.forEach(linkItem => {
                    console.log(linkItem)
                    if(linkItem.href.indexOf("/jumpGo") == -1) {
                        let checkDomain = urlDomainReg.exec(linkItem.href)[0];
                        if (linkItem.getAttribute("type") == "download") {
                            let downloadParam = linkItem.href.toString().indexOf("?") == -1 ? "?type=goDown" : "&type=goDown";
                            linkItem.href = "/jumpGo?goUrl=" + encodeURIComponent(linkItem.href) + downloadParam;
                        } else {
                            if (checkDomain != null && checkDomain != "") {
                                let isWhiteList = whiteListDetect(whiteListDatas, checkDomain);
                                linkItem.href = isWhiteList ? linkItem.href : "/jumpGo?goUrl=" + encodeURIComponent(linkItem.href);
                            } else {
                                linkItem.href = "/jumpGo?goUrl=" + encodeURIComponent(linkItem.href);
                                linkItem.target = "_blank";
                            }
                        }
                    }
                });
            }
        })

    }

    function  whiteListDetect(whiteListDatas, checkDomain){
        // 白名单匹配
        let patchRes = whiteListDatas.filter(globalLinkItem => {
            if(globalLinkItem.indexOf("*") !== -1){
                if(isDomainInWildcard(checkDomain, globalLinkItem)) return globalLinkItem;
            }else{
                if(checkDomain == globalLinkItem) return  globalLinkItem;
            }
        })
        return patchRes.length > 0 ? true : false;
    }

    // 泛域名匹配
    function isDomainInWildcard(domain, wildcard) {
        if(domain == wildcard.replace("*.","")){
            return true;
        }
        const domainParts = domain.split('.');
        const wildcardParts = wildcard.split('.');

        if (domainParts.length !== wildcardParts.length) {
            return false;
        }

        for (let i = 0; i < domainParts.length; i++) {
            if (wildcardParts[i] === '*') {
                continue;
            }
            if (domainParts[i] !== wildcardParts[i]) {
                return false;
            }
        }
        return true;
    }
    async function fetchWhiteList(){
        let responseBody = await fetch("/apis/core.wenjing.xin/v1alpha1/lsdConfig/white-list");
        let data = await responseBody.json();
        return data;
    }
    window.commentLinkScan = commentLinkScan;
})();