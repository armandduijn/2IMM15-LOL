<?php

namespace App;

class Helper
{

    /**
     * Runs a Python command
     *
     * @param string $script
     * @param string $argument
     * @return string
     */
    public static function runCommand(string $script, string $argument): string
    {
        $command = sprintf(
            'python "%s/../../../modules/view-helpers/%s" %s',
            __DIR__,
            $script,
            escapeshellarg($argument)
        );

        return shell_exec($command);
    }

    /**
     * Runs a Python module
     *
     * @param string $module
     * @param string $argument
     * @return array
     */
    public static function runOnServer(string $module, string $argument): array
    {
        try {
            $response = file_get_contents(
                sprintf('http://localhost:5000?module=%s&argument=%s', $module, $argument)
            );
        } catch (\Exception $e) {
            // noop
        }

        if (empty($response)) {
            return [];
        }

        return json_decode($response, true);
    }

    /**
     * Render a template
     *
     * @param string $template Absolute path to the template file
     * @param array  $data     Data to be passed to the template file
     *
     * @return string
     */
    public static function render(string $template, array $data = []): string
    {
        ob_start();

        extract($data);
        include $template;

        return ob_get_clean();
    }
}
