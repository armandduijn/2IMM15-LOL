<?php

namespace App;

use Slim\Container as SlimContainer;

class Container
{
    /**
     * @var \Slim\Container
     */
    protected static $container;

    /**
     * @return \Slim\Container
     */
    public static function getContainer()
    {
        return self::$container;
    }

    /**
     * @param \Slim\Container $container
     */
    public static function setContainer(SlimContainer $container)
    {
        self::$container = $container;
    }
}
