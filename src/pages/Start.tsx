import { useEffect, useState } from "react"
import CreateUrl from "./CreateUrl"
import "../css/Start.css"
import copy from "../photos/copy.png";


function Start() {
    const [shortUrl, setShortUrl] = useState("");
    const [copied, setCopied] = useState(false);

    async function handleCopy() {
        try {
            await navigator.clipboard.writeText(shortUrl);
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        } catch (err) {
            console.error("Copy failed", err);
        }
    }

    return (
        <div>
            <h3>Shorten your links here!</h3>
            <CreateUrl onCreated={setShortUrl} />

            {shortUrl && (
                <div className="resultBox">
                    <h2>Your short url:</h2>
                    <div className="resultInnerBox">
                        <a href={shortUrl} target="_blank" rel="noreferrer">{shortUrl} </a>
                        <button className="copyBtn" onClick={handleCopy}> 
                            <img src={copy} alt="copy" /> 
                        </button>
                    </div>
                    {copied && <p className="copiedMsg">Copied!</p>}
                </div>
            )}
        </div>
  );
}

export default Start;