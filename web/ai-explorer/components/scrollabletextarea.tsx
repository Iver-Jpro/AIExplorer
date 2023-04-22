import { useEffect, useRef, useState } from "react";
import { useQuery } from "react-query";

interface ExploreResponse {
    description: string;
    imageUrl: string;
    nextLocations: string[];
}

function ScrollableTextArea(): JSX.Element {
    const [text, setText] = useState<string>("");
    const textAreaRef = useRef<HTMLDivElement>(null);
    const [buttonClicked, setButtonClicked] = useState<boolean>(false);
    const { data, isLoading, error, refetch } = useQuery<ExploreResponse>(
        "explore",
        () => {
            if (text.trim()) {
                return fetch("http://localhost:8080/api/explore", {
                    method: "POST",
                    body: JSON.stringify(text),
                    headers: {
                        "Content-Type": "application/json",
                    },
                }).then((res) => res.json());
            } else {
                return Promise.resolve({
                    description: "",
                    imageUrl: "",
                    nextLocations: [],
                });
            }
        }
    );

    useEffect(() => {
        if (textAreaRef.current) {
            textAreaRef.current.scrollTop = textAreaRef.current.scrollHeight;
        }
    }, [data]);

    const handleTextChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setText(event.target.value);
    };

    const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") {
            event.preventDefault();
            if (text.trim()) {
                refetch();
            }
        }
    };

    const handleButtonClick = (nextLocation: string) => {
        setButtonClicked(true);
        setText(nextLocation);
    };

    const handleResetClick = async () => {
        try {
            await fetch("http://localhost:8080/api/reset");
            window.location.reload();
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => {
        if (buttonClicked&&text.trim()) {
            setButtonClicked(false);
            refetch();
        }
    }, [text]);

    return (
        <div style={{ display: "flex" }}>
            <div
                style={{
                    flexGrow: 1,
                    height: "100vh",
                    overflowY: "scroll",
                }}
                ref={textAreaRef}
            >
                {isLoading
                    ? "Loading..."
                    : error
                        ? "Error fetching data"
                        : data?.description || ""}
            </div>
            <div>
                {data && !isLoading && !error && (
                    <>
                        <input
                        type="text"
                        placeholder="Type something here..."
                        value={text}
                        onChange={handleTextChange}
                        onKeyDown={handleKeyDown}
                        style={{
                            height: "3rem",
                            padding: "0.5rem",
                            borderTop: "1px solid #ccc",
                        }}
                    />
                        <div>
                            <button onClick={handleResetClick}>Reset</button>
                        </div>
                        <img
                            src={data.imageUrl}
                            alt="Exploration image"
                            style={{ maxWidth: "100%" }}
                        />
                        <div style={{ whiteSpace: "pre-line" }}>
                            {data.nextLocations.map((nextLocation: string) => (
                                <button
                                    key={nextLocation}
                                    onClick={() => handleButtonClick(nextLocation)}
                                    style={{ display: "block", marginBottom: "5px" }}
                                >
                                    {nextLocation}
                                </button>
                            ))}
                        </div>
                    </>
                )}
            </div>



        </div>

    );
}

export default ScrollableTextArea;
