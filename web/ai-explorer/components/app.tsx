import React from 'react';
import {QueryClient, QueryClientProvider} from 'react-query';
import ScrollableTextAreaOld from "@/components/scrollableTextAreaOld";
import ScrollableTextArea from "@/components/scrollabletextarea";


const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            refetchIntervalInBackground: false,
        },
    },
});

function App() {
    return (
        <React.StrictMode>
            <QueryClientProvider client={queryClient}>
                <ScrollableTextArea/>
            </QueryClientProvider>
        </React.StrictMode>
    );
}

export default App;
