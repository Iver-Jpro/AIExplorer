import React, { useState, useEffect, useRef } from 'react';
import { useQuery } from 'react-query';

function ScrollableTextAreaOld(): JSX.Element {
    const [text, setText] = useState<string>('');
    const textAreaRef = useRef<HTMLDivElement>(null);

    const { data, isLoading, error, refetch } = useQuery<string>('explore', () =>
        fetch('http://localhost:8080/api/explore', {
            method: 'POST',
            body: JSON.stringify(text),
            headers: {
                'Content-Type': 'application/json',
            },
        }).then((res) => res.text())
    );

    useEffect(() => {
        if (textAreaRef.current) {
            textAreaRef.current.scrollTop = textAreaRef.current.scrollHeight;
        }
    }, [text]);

    const handleTextChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setText(event.target.value);
    };

    const handleKeyDown = async (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === 'Enter') {
            event.preventDefault(); // Prevent the default behavior of the Enter key (e.g. submitting a form)
            if (text.trim()) {
                await refetch(); // Call the API to fetch the results
                setText(''); // Reset the text field
            }
        }
    };

    return (
        <div
            style={{
                height: '100vh',
                display: 'flex',
                flexDirection: 'column',
            }}
        >
            <div
                style={{
                    flexGrow: 1,
                    overflowY: 'scroll',
                }}
                ref={textAreaRef}
            >
                {isLoading ? 'Loading...' : error ? 'Error fetching data' : data}
            </div>
            <input
                type="text"
                placeholder="Type something here..."
                value={text}
                onChange={handleTextChange}
                onKeyDown={handleKeyDown}
                style={{
                    height: '3rem',
                    padding: '0.5rem',
                    borderTop: '1px solid #ccc',
                }}
            />
        </div>
    );
}

export default ScrollableTextAreaOld;
